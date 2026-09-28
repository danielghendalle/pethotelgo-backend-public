#!/usr/bin/env bash
#
# Provision the PetHotelGO database as an OCI MySQL HeatWave DB System on the
# Always Free "MySQL.Free" shape (1 OCPU / 8 GB, managed: automatic backups,
# patching). MySQL DB Systems have no public IP — the API reaches it over the
# VCN's private network.
#
# Prereqs: oci-cli configured.
#
# Usage:
#   DB_PASSWORD='<8-32 chars, upper+lower+digit+special>' \
#   COMPARTMENT_OCID='ocid1.tenancy.oc1..xxxx' \
#   SUBNET_OCID='ocid1.subnet.oc1.sa-saopaulo-1.xxxx' \
#   AVAILABILITY_DOMAIN='QtKm:SA-SAOPAULO-1-AD-1' \
#   SECURITY_LIST_OCID='ocid1.securitylist.oc1.sa-saopaulo-1.xxxx' \
#   VCN_CIDR='10.0.0.0/16' \
#   ./scripts/provision-mysql-oci.sh
#
# The values used for the current deployment are in DEPLOY_BACKEND.md.
set -euo pipefail

: "${DB_PASSWORD:?set DB_PASSWORD}"
: "${COMPARTMENT_OCID:?set COMPARTMENT_OCID}"
: "${SUBNET_OCID:?set SUBNET_OCID}"
: "${AVAILABILITY_DOMAIN:?set AVAILABILITY_DOMAIN}"
VCN_CIDR="${VCN_CIDR:-10.0.0.0/16}"
NAME="${NAME:-pethotelgo-mysql}"
STORAGE_GB="${STORAGE_GB:-50}"

# Always Free MySQL is pinned to the latest version; ask the API for it.
MYSQL_VERSION="$(oci mysql version list -c "$COMPARTMENT_OCID" \
  --query 'data[-1].versions[-1].version' --raw-output)"
echo "mysql version: $MYSQL_VERSION"

# --- security list: allow 3306/33060 from the VCN --------------------------
if [ -n "${SECURITY_LIST_OCID:-}" ]; then
  workdir="$(mktemp -d)"; trap 'rm -rf "$workdir"' EXIT
  oci network security-list get --security-list-id "$SECURITY_LIST_OCID" \
    --query 'data."ingress-security-rules"' > "$workdir/ingress.json"
  python3 - "$workdir/ingress.json" "$VCN_CIDR" > "$workdir/ingress-new.json" <<'PY'
import json, sys
rules = json.load(open(sys.argv[1])); cidr = sys.argv[2]
km = {"icmp-options":"icmpOptions","is-stateless":"isStateless","source-type":"sourceType",
      "tcp-options":"tcpOptions","udp-options":"udpOptions",
      "destination-port-range":"destinationPortRange","source-port-range":"sourcePortRange"}
def conv(x):
    if isinstance(x, dict):  return {km.get(k,k): conv(v) for k,v in x.items()}
    if isinstance(x, list):  return [conv(i) for i in x]
    return x
out = [conv(r) for r in rules]
def has(port): return any((r.get("tcpOptions") or {}).get("destinationPortRange",{}).get("min")==port for r in out)
for port, desc in ((3306,"MySQL from VCN"), (33060,"MySQL X protocol from VCN")):
    if not has(port):
        out.append({"description":desc,"protocol":"6","source":cidr,"sourceType":"CIDR_BLOCK","isStateless":False,
                    "tcpOptions":{"destinationPortRange":{"min":port,"max":port},"sourcePortRange":None},
                    "udpOptions":None,"icmpOptions":None})
json.dump(out, sys.stdout)
PY
  oci network security-list update --security-list-id "$SECURITY_LIST_OCID" \
    --ingress-security-rules "file://$workdir/ingress-new.json" --force >/dev/null
  echo "security list: 3306/33060 ingress from $VCN_CIDR ensured"
fi

# --- create the DB system -------------------------------------------------
echo "creating MySQL DB system $NAME (MySQL.Free) ..."
oci mysql db-system create \
  -c "$COMPARTMENT_OCID" \
  --display-name "$NAME" \
  --shape-name MySQL.Free \
  --subnet-id "$SUBNET_OCID" \
  --availability-domain "$AVAILABILITY_DOMAIN" \
  --mysql-version "$MYSQL_VERSION" \
  --admin-username pethotel \
  --admin-password "$DB_PASSWORD" \
  --data-storage-size-in-gbs "$STORAGE_GB" \
  --is-highly-available false \
  --hostname-label "$NAME" \
  --wait-for-state SUCCEEDED --max-wait-seconds 2400 \
  --query 'data.{id:id,state:"lifecycle-state"}'

echo
echo "Get the private endpoint hostname:"
echo "  oci mysql db-system get --db-system-id <id> --query 'data.endpoints[0].hostname'"
echo "Then set in the backend .env:"
echo "  JDBC_DATABASE_URL=jdbc:mysql://<hostname>:3306/pethotel"
echo "  JDBC_DATABASE_USERNAME=pethotel"
echo "  JDBC_DATABASE_PASSWORD=<DB_PASSWORD>"
echo
echo "Create the application schema once (from the API instance, which can reach"
echo "the private endpoint):"
echo "  mysqlsh pethotel@<hostname> --sql -e \"CREATE DATABASE IF NOT EXISTS pethotel\""
echo "or add ?createDatabaseIfNotExist=true to JDBC_DATABASE_URL for the first boot."
