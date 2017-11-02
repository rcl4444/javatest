#!/bin/sh

# fix config.yml using env vars, possible envs
#
# JDBC Related:
#
#   * JDBC_DRIVER (org.h2.Driver)
#   * JDBC_USER (sa)
#   * JDBC_PASSWORD ("")
#   * JDBC_URL (jdbc:h2:./target/test)
#
# Host:
#   * FRONTEND_URL (https://passport.yunbaoguan.cn)
#   * BACKEND_ENDPOINT (https://ppbe.yunbaoguan.cn)
#
JDBC_DRIVER=${JDBC_DRIVER:=org.h2.Driver}
JDBC_USER=${JDBC_USER:=sa}
JDBC_PASSWORD=${JDBC_PASSWORD:=}
JDBC_URL=${JDBC_URL:=jdbc:h2:./target/test}

FRONTEND_URL=${FRONTEND_URL:=https://passport.yunbaoguan.cn}
BACKEND_ENDPOINT=${BACKEND_ENDPOINT:=https://ppbe.yunbaoguan.cn}

sed "s|__JDBC_DRIVER__|$JDBC_DRIVER|g" config.yml.tmpl > config.yml
sed -i "s|__JDBC_USER__|$JDBC_USER|g" config.yml
sed -i "s|__JDBC_PASSWORD__|$JDBC_PASSWORD|g" config.yml
sed -i "s|__JDBC_URL__|$JDBC_URL|g" config.yml
sed -i "s|__FRONTEND_URL__|$FRONTEND_URL|g" config.yml
sed -i "s|__BACKEND_ENDPOINT__|$BACKEND_ENDPOINT|g" config.yml

export DB_CONTEXT=${DB_CONTEXT:=prod}

exec "$@"
