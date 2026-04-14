#!/usr/bin/env zsh
# ==============================================================
# EvolveHub - 一键构建 & 部署脚本
# ==============================================================
# Usage:
#   ./deploy.sh                 # 构建全部服务并部署
#   ./deploy.sh build           # 仅构建镜像（不部署）
#   ./deploy.sh push            # 构建并推送到阿里云镜像仓库
#   ./deploy.sh push auth admin # 只推送指定服务
# ==============================================================

set -e

# -------------------- 加载 .env --------------------
SCRIPT_DIR="${0:A:h}"
if [[ -f "${SCRIPT_DIR}/.env" ]]; then
    set -a
    source "${SCRIPT_DIR}/.env"
    set +a
fi

# -------------------- 配置区 --------------------
REGISTRY="${DOCKER_REGISTRY:-}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
COMPOSE_FILE="docker-compose-services.yml"

# 服务定义（顺序数组，name:module:port）
ALL_SERVICES=(
    "gateway:evolvehub-gateway:8080"
    "auth:evolvehub-auth:8081"
    "ai:evolvehub-ai:8082"
    "admin:evolvehub-admin:8083"
)

# -------------------- 工具函数 --------------------
log()  { echo "\033[0;36m[EvolveHub]\033[0m $1"; }
ok()   { echo "\033[0;32m[✓]\033[0m $1"; }
err()  { echo "\033[0;31m[✗]\033[0m $1"; exit 1; }

get_module() { echo "$1" | cut -d: -f2; }
get_port()   { echo "$1" | cut -d: -f3; }
get_name()   { echo "$1" | cut -d: -f1; }

# -------------------- 解析参数 --------------------
ACTION="deploy"
TARGET_NAMES=()

for arg in "$@"; do
    case "$arg" in
        build) ACTION="build" ;;
        push)  ACTION="push" ;;
        *)     TARGET_NAMES+=("$arg") ;;
    esac
done

# 解析目标服务
TARGET_SERVICES=()
if [[ ${#TARGET_NAMES[@]} -eq 0 ]]; then
    TARGET_SERVICES=("${ALL_SERVICES[@]}")
else
    for name in "${TARGET_NAMES[@]}"; do
        local found=0
        for entry in "${ALL_SERVICES[@]}"; do
            if [[ "$(get_name $entry)" == "$name" ]]; then
                TARGET_SERVICES+=("$entry")
                found=1
                break
            fi
        done
        [[ $found -eq 0 ]] && err "未知服务: $name  可选: gateway auth ai admin"
    done
fi

# -------------------- Step 1: Maven 构建 --------------------
log "Step 1/3 - Maven 打包..."

MODULES=""
for entry in "${TARGET_SERVICES[@]}"; do
    MODULES="${MODULES},$(get_module $entry)"
done
MODULES="${MODULES:1}"

mvn clean package -pl "${MODULES}" -am -DskipTests -B -q
ok "Maven 打包完成"

# -------------------- Step 2: Docker 构建 --------------------
log "Step 2/3 - Docker 镜像构建..."

for entry in "${TARGET_SERVICES[@]}"; do
    local module=$(get_module $entry)
    local name=$(get_name $entry)

    if [[ -n "$REGISTRY" ]]; then
        IMAGE_NAME="${REGISTRY}/${module}:${IMAGE_TAG}"
    else
        IMAGE_NAME="${module}:${IMAGE_TAG}"
    fi

    log "  构建 ${name} → ${IMAGE_NAME}"
    docker build --platform linux/amd64 --build-arg MODULE="${module}" -t "${IMAGE_NAME}" .
    ok "  ${name} 镜像构建完成"
done

# -------------------- Step 3: 部署/推送 --------------------
if [[ "$ACTION" == "push" ]]; then
    [[ -z "$REGISTRY" ]] && err "DOCKER_REGISTRY 未配置，无法推送。请在 .env 中设置"

    REGISTRY_HOST=$(echo "$REGISTRY" | cut -d'/' -f1)
    log "Step 3/3 - 登录 ${REGISTRY_HOST} ..."
    docker login "${REGISTRY_HOST}"

    log "推送镜像..."
    for entry in "${TARGET_SERVICES[@]}"; do
        local module=$(get_module $entry)
        local name=$(get_name $entry)
        IMAGE_NAME="${REGISTRY}/${module}:${IMAGE_TAG}"
        log "  推送 ${IMAGE_NAME}"
        docker push "${IMAGE_NAME}"
        ok "  ${name} 推送完成"
    done

    echo ""
    log "推送完成，服务器拉取命令:"
    for entry in "${TARGET_SERVICES[@]}"; do
        local module=$(get_module $entry)
        echo "  docker pull ${REGISTRY}/${module}:${IMAGE_TAG}"
    done

elif [[ "$ACTION" == "deploy" ]]; then
    log "Step 3/3 - 启动服务..."

    COMPOSE_SERVICES=""
    for entry in "${TARGET_SERVICES[@]}"; do
        COMPOSE_SERVICES="${COMPOSE_SERVICES} evolvehub-$(get_name $entry)"
    done

    docker compose -f "${COMPOSE_FILE}" up -d ${=COMPOSE_SERVICES}
    ok "服务已启动"

    echo ""
    log "服务状态:"
    for entry in "${TARGET_SERVICES[@]}"; do
        echo "  ● evolvehub-$(get_name $entry)  →  http://localhost:$(get_port $entry)"
    done
else
    ok "镜像构建完成（仅构建模式，未部署）"
fi

echo ""
ok "全部完成！"
