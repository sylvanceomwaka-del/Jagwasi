#!/data/data/com.termux/files/usr/bin/bash

# =====================================================
# Jagwasi - Termux POS & Management System
# Standalone Bash script (Requires only jq and standard coreutils)
# Data stored in ~/.jagwasi/
# =====================================================

set -e
set -u

DATA_DIR="$HOME/.jagwasi"
mkdir -p "$DATA_DIR"

CURRENT_USER_NAME=""
CURRENT_USER_ROLE=""
CURRENT_USER_ID=""

# ------------------ Initialization ------------------
init_data() {
    local changed=0

    # Staff file
    if [[ ! -f "$DATA_DIR/staff.json" ]]; then
        echo '[]' > "$DATA_DIR/staff.json"
        local master_pin_hash=$(echo -n "1234" | sha256sum | cut -d' ' -f1)
        jq --arg hash "$master_pin_hash" \
           '. + [{"id": 1, "name": "Master", "role": "MASTER", "pinHash": $hash, "active": true}]' \
           "$DATA_DIR/staff.json" > "$DATA_DIR/staff.json.tmp"
        mv "$DATA_DIR/staff.json.tmp" "$DATA_DIR/staff.json"
        changed=1
    fi

    # Stock file
    if [[ ! -f "$DATA_DIR/stock.json" ]]; then
        echo '[]' > "$DATA_DIR/stock.json"
        jq '. + [{"id": 1, "name": "Rice", "quantity": 50, "unit": "kg", "threshold": 10, "cost": 1.5},
                 {"id": 2, "name": "Beans", "quantity": 30, "unit": "kg", "threshold": 5, "cost": 2.0},
                 {"id": 3, "name": "Cooking Oil", "quantity": 20, "unit": "liters", "threshold": 4, "cost": 3.5}]' \
           "$DATA_DIR/stock.json" > "$DATA_DIR/stock.json.tmp"
        mv "$DATA_DIR/stock.json.tmp" "$DATA_DIR/stock.json"
        changed=1
    fi

    # Initialize empty data files
    [[ ! -f "$DATA_DIR/orders.json" ]] && echo '[]' > "$DATA_DIR/orders.json"
    [[ ! -f "$DATA_DIR/payments.json" ]] && echo '[]' > "$DATA_DIR/payments.json"
    [[ ! -f "$DATA_DIR/audit.json" ]] && echo '[]' > "$DATA_DIR/audit.json"

    if [[ $changed -eq 1 ]]; then
        echo "Initial data created (Master PIN: 1234)."
    fi
}

# ------------------ Helper Functions ------------------
get_next_id() {
    local file="$1"
    if [[ -f "$file" ]]; then
        local max_id
        max_id=$(jq 'map(.id) | max // 0' "$file")
        echo $((max_id + 1))
    else
        echo 1
    fi
}

log_audit() {
    local actor="$1"
    local action="$2"
    local details="$3"
    local id
    id=$(get_next_id "$DATA_DIR/audit.json")
    local ts
    ts=$(date +%s)
    
    jq --arg id "$id" --arg actor "$actor" --arg action "$action" --arg details "$details" --arg ts "$ts" \
       '. + [{"id": ($id|tonumber), "actor": $actor, "action": $action, "details": $details, "timestamp": ($ts|tonumber)}]' \
       "$DATA_DIR/audit.json" > "$DATA_DIR/audit.json.tmp"
    mv "$DATA_DIR/audit.json.tmp" "$DATA_DIR/audit.json"
}

staff_login() {
    echo -n "Enter Staff Name: "
    read -r name
    echo -n "Enter PIN: "
    read -rs pin
    echo
    local pin_hash
    pin_hash=$(echo -n "$pin" | sha256sum | cut -d' ' -f1)
    
    local staff
    staff=$(jq --arg name "$name" --arg hash "$pin_hash" \
               '.[] | select(.name == $name and .pinHash == $hash and .active == true)' \
               "$DATA_DIR/staff.json" 2>/dev/null || true)

    if [[ -n "$staff" ]]; then
        echo "Login successful."
        CURRENT_USER_NAME=$(echo "$staff" | jq -r '.name')
        CURRENT_USER_ROLE=$(echo "$staff" | jq -r '.role')
        CURRENT_USER_ID=$(echo "$staff" | jq -r '.id')
        log_audit "$CURRENT_USER_NAME" "LOGIN" "Staff logged in"
        return 0
    else
        echo "Invalid credentials or inactive account."
        return 1
    fi
}

# ------------------ Stock Management ------------------
list_stock() {
    jq -r '.[] | "\(.id): \(.name) - \(.quantity) \(.unit) (threshold: \(.threshold)) - $\(.cost)/\(.unit)"' "$DATA_DIR/stock.json"
}

add_stock() {
    if [[ "$CURRENT_USER_ROLE" != "MASTER" ]]; then
        echo "Only MASTER can add stock items."
        return
    fi
    echo -n "Item name: "; read -r name
    echo -n "Quantity: "; read -r quantity
    echo -n "Unit (e.g., kg, pcs): "; read -r unit
    echo -n "Threshold: "; read -r threshold
    echo -n "Cost per unit: "; read -r cost
    
    local id
    id=$(get_next_id "$DATA_DIR/stock.json")
    jq --arg id "$id" --arg name "$name" --arg quantity "$quantity" --arg unit "$unit" --arg threshold "$threshold" --arg cost "$cost" \
       '. + [{"id": ($id|tonumber), "name": $name, "quantity": ($quantity|tonumber), "unit": $unit, "threshold": ($threshold|tonumber), "cost": ($cost|tonumber)}]' \
       "$DATA_DIR/stock.json" > "$DATA_DIR/stock.json.tmp"
    mv "$DATA_DIR/stock.json.tmp" "$DATA_DIR/stock.json"
    
    log_audit "$CURRENT_USER_NAME" "STOCK_ADD" "Added item: $name"
    echo "Stock item added successfully."
}

deduct_stock() {
    list_stock
    echo -n "Enter item ID to deduct: "
    read -r item_id
    echo -n "Quantity to deduct: "
    read -r qty
    
    local item
    item=$(jq --arg id "$item_id" '.[] | select(.id == ($id|tonumber))' "$DATA_DIR/stock.json" 2>/dev/null || true)
    if [[ -z "$item" ]]; then
        echo "Item not found."
        return
    fi

    local current_qty
    current_qty=$(echo "$item" | jq -r '.quantity')
    
    local is_insufficient
    is_insufficient=$(jq -n --arg curr "$current_qty" --arg deduct "$qty" '$curr | tonumber < ($deduct | tonumber)')
    
    if [[ "$is_insufficient" == "true" ]]; then
        echo "Insufficient stock. Current quantity: $current_qty"
        return
    fi

    local item_name
    item_name=$(echo "$item" | jq -r '.name')
    
    jq --arg id "$item_id" --arg qty "$qty" \
       'map(if .id == ($id|tonumber) then .quantity = (.quantity - ($qty|tonumber)) else . end)' \
       "$DATA_DIR/stock.json" > "$DATA_DIR/stock.json.tmp"
    mv "$DATA_DIR/stock.json.tmp" "$DATA_DIR/stock.json"
    
    log_audit "$CURRENT_USER_NAME" "STOCK_DEDUCT" "Deducted $qty $item_name"
    echo "Stock updated."
}

restock() {
    if [[ "$CURRENT_USER_ROLE" != "MASTER" ]]; then
        echo "Only MASTER can restock."
        return
    fi
    list_stock
    echo -n "Enter item ID to restock: "
    read -r item_id
    echo -n "Quantity to add: "
    read -r qty
    
    local item
    item=$(jq --arg id "$item_id" '.[] | select(.id == ($id|tonumber))' "$DATA_DIR/stock.json" 2>/dev/null || true)
    if [[ -z "$item" ]]; then
        echo "Item not found."
        return
    fi

    local item_name
    item_name=$(echo "$item" | jq -r '.name')
    
    jq --arg id "$item_id" --arg qty "$qty" \
       'map(if .id == ($id|tonumber) then .quantity = (.quantity + ($qty|tonumber)) else . end)' \
       "$DATA_DIR/stock.json" > "$DATA_DIR/stock.json.tmp"
    mv "$DATA_DIR/stock.json.tmp" "$DATA_DIR/stock.json"
    
    log_audit "$CURRENT_USER_NAME" "STOCK_RESTOCK" "Restocked $qty $item_name"
    echo "Stock restocked successfully."
}

# ------------------ Order & Payment ------------------
create_order() {
    echo -n "Customer name: "
    read -r customer
    echo "Available stock:"
    list_stock
    
    local order_items="[]"
    local total=0

    while true; do
        echo -n "Enter item ID (or 0 to finish): "
        read -r item_id
        [[ "$item_id" == "0" ]] && break
        
        echo -n "Quantity: "
        read -r qty
        
        local item
        item=$(jq --arg id "$item_id" '.[] | select(.id == ($id|tonumber))' "$DATA_DIR/stock.json" 2>/dev/null || true)
        if [[ -z "$item" ]]; then
            echo "Item not found."
            continue
        fi

        local stock_qty
        stock_qty=$(echo "$item" | jq -r '.quantity')
        local insufficient
        insufficient=$(jq -n --arg curr "$stock_qty" --arg req "$qty" '$curr | tonumber < ($req | tonumber)')

        if [[ "$insufficient" == "true" ]]; then
            echo "Not enough stock. Available: $stock_qty"
            continue
        fi

        local name cost subtotal
        name=$(echo "$item" | jq -r '.name')
        cost=$(echo "$item" | jq -r '.cost')
        subtotal=$(jq -n --arg q "$qty" --arg c "$cost" '($q | tonumber) * ($c | tonumber)')
        total=$(jq -n --arg t "$total" --arg s "$subtotal" '($t | tonumber) + ($s | tonumber)')

        order_items=$(echo "$order_items" | jq --arg name "$name" --arg qty "$qty" --arg cost "$cost" \
            '. + [{"name": $name, "quantity": ($qty|tonumber), "price": ($cost|tonumber)}]')

        # Deduct stock
        jq --arg id "$item_id" --arg qty "$qty" \
           'map(if .id == ($id|tonumber) then .quantity = (.quantity - ($qty|tonumber)) else . end)' \
           "$DATA_DIR/stock.json" > "$DATA_DIR/stock.json.tmp"
        mv "$DATA_DIR/stock.json.tmp" "$DATA_DIR/stock.json"
    done

    if [[ "$order_items" == "[]" ]]; then
        echo "No items added."
        return
    fi

    local order_id ts
    order_id=$(get_next_id "$DATA_DIR/orders.json")
    ts=$(date +%s)

    jq --arg id "$order_id" --arg customer "$customer" --arg items "$order_items" --arg total "$total" --arg ts "$ts" --arg staff "$CURRENT_USER_NAME" \
       '. + [{"id": ($id|tonumber), "customer": $customer, "items": ($items|fromjson), "total": ($total|tonumber), "status": "PENDING", "timestamp": ($ts|tonumber), "staff": $staff}]' \
       "$DATA_DIR/orders.json" > "$DATA_DIR/orders.json.tmp"
    mv "$DATA_DIR/orders.json.tmp" "$DATA_DIR/orders.json"

    log_audit "$CURRENT_USER_NAME" "ORDER_CREATE" "Order #$order_id for $customer total: $total"
    echo "Order #$order_id created. Total: $total"
}

process_payment() {
    echo -n "Enter Order ID to pay: "
    read -r order_id
    
    local order
    order=$(jq --arg id "$order_id" '.[] | select(.id == ($id|tonumber) and .status == "PENDING")' "$DATA_DIR/orders.json" 2>/dev/null || true)
    if [[ -z "$order" ]]; then
        echo "Order not found or already paid."
        return
    fi

    local total
    total=$(echo "$order" | jq -r '.total')
    echo "Order total: $total"
    
    echo -n "Payment method (CASH/M-PESA/CARD): "
    read -r method
    echo -n "Amount received: "
    read -r amount; amount=$(echo "$amount" | tr -d "$")

    local is_short
    is_short=$(jq -n --arg amt "$amount" --arg tot "$total" '$amt | tonumber < ($tot | tonumber)')
    
    local debt=0
    if [[ "$is_short" == "true" ]]; then
        debt=$(jq -n --arg tot "$total" --arg amt "$amount" '($tot | tonumber) - ($amt | tonumber)')
        echo "Amount less than total. Outstanding balance: $debt"
    fi

    local pay_id ts status trans_id
    pay_id=$(get_next_id "$DATA_DIR/payments.json")
    ts=$(date +%s)
    status="COMPLETED"
    trans_id="TXN-$(date +%s)-$RANDOM"

    jq --arg id "$pay_id" --arg oid "$order_id" --arg amount "$amount" --arg method "$method" --arg status "$status" --arg trans "$trans_id" --arg ts "$ts" \
       '. + [{"id": ($id|tonumber), "orderId": ($oid|tonumber), "amount": ($amount|tonumber), "method": $method, "status": $status, "transactionId": $trans, "timestamp": ($ts|tonumber)}]' \
       "$DATA_DIR/payments.json" > "$DATA_DIR/payments.json.tmp"
    mv "$DATA_DIR/payments.json.tmp" "$DATA_DIR/payments.json"

    jq --arg id "$order_id" \
       'map(if .id == ($id|tonumber) then .status = "PAID" else . end)' \
       "$DATA_DIR/orders.json" > "$DATA_DIR/orders.json.tmp"
    mv "$DATA_DIR/orders.json.tmp" "$DATA_DIR/orders.json"

    log_audit "$CURRENT_USER_NAME" "PAYMENT" "Order #$order_id paid via $method"
    echo "Payment recorded successfully. Outstanding balance: $debt"
}

# ------------------ Reports ------------------
show_receipt() {
    echo -n "Enter Order ID: "
    read -r order_id
    
    local order
    order=$(jq --arg id "$order_id" '.[] | select(.id == ($id|tonumber))' "$DATA_DIR/orders.json" 2>/dev/null || true)
    if [[ -z "$order" ]]; then
        echo "Order not found."
        return
    fi

    echo "========================================"
    echo "           JAGWASI RECEIPT"
    echo "Order ID : #$order_id"
    echo "Customer : $(echo "$order" | jq -r '.customer')"
    echo "Staff    : $(echo "$order" | jq -r '.staff')"
    echo "Date     : $(date "+%Y-%m-%d %H:%M:%S" --date=@$(echo "$order" | jq -r '.timestamp') 2>/dev/null || date)"
    echo "----------------------------------------"
    echo "Items:"
    echo "$order" | jq -r '.items[] | "  \(.quantity) x \(.name) @ $\(.price) = $\(.quantity * .price)"'
    echo "----------------------------------------"
    echo "Total    : $(echo "$order" | jq -r '.total')"
    echo "Status   : $(echo "$order" | jq -r '.status')"
    echo "========================================"
}

audit_log() {
    echo "Recent audit entries (last 20):"
    jq -r 'reverse | .[0:20] | .[] | "[\(.timestamp)] \(.actor): \(.action) - \(.details)"' "$DATA_DIR/audit.json" 2>/dev/null || echo "No entries found."
}

# ------------------ Main Menu ------------------
main_menu() {
    while true; do
        echo ""
        echo "=================================="
        echo "        JAGWASI - Main Menu"
        if [[ -n "$CURRENT_USER_NAME" ]]; then
            echo "  Logged in as: $CURRENT_USER_NAME ($CURRENT_USER_ROLE)"
        else
            echo "  [ Not Logged In ]"
        fi
        echo "=================================="
        echo "1. Staff Login"
        echo "2. Create Order"
        echo "3. Process Payment"
        echo "4. Manage Stock"
        echo "5. View Audit Log"
        echo "6. Generate Receipt"
        echo "7. Logout"
        echo "8. Exit"
        echo "=================================="
        echo -n "Choose [1-8]: "
        read -r choice
        case $choice in
            1) staff_login ;;
            2) [[ -z "$CURRENT_USER_NAME" ]] && echo "Please login first." || create_order ;;
            3) [[ -z "$CURRENT_USER_NAME" ]] && echo "Please login first." || process_payment ;;
            4)
                if [[ -z "$CURRENT_USER_NAME" ]]; then
                    echo "Please login first."
                else
                    echo "--- Stock Submenu ---"
                    echo "1. List stock"
                    echo "2. Add stock item (MASTER only)"
                    echo "3. Deduct stock"
                    echo "4. Restock (MASTER only)"
                    echo -n "Choose [1-4]: "
                    read -r sub
                    case $sub in
                        1) list_stock ;;
                        2) add_stock ;;
                        3) deduct_stock ;;
                        4) restock ;;
                        *) echo "Invalid option." ;;
                    esac
                fi
                ;;
            5) audit_log ;;
            6) show_receipt ;;
            7)
                if [[ -n "$CURRENT_USER_NAME" ]]; then
                    log_audit "$CURRENT_USER_NAME" "LOGOUT" "Logged out"
                    CURRENT_USER_NAME=""
                    CURRENT_USER_ROLE=""
                    CURRENT_USER_ID=""
                    echo "Logged out."
                else
                    echo "Not logged in."
                fi
                ;;
            8) echo "Goodbye."; exit 0 ;;
            *) echo "Invalid choice." ;;
        esac
        echo -n "Press Enter to continue..."
        read -r
    done
}

# ------------------ Start Execution ------------------
init_data
main_menu
