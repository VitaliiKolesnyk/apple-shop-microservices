FROM quay.io/keycloak/keycloak:latest as builder

WORKDIR /opt/keycloak

RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:latest
COPY --from=builder /opt/keycloak/ /opt/keycloak/

COPY ./themes/my-theme/ /opt/keycloak/themes/my-theme

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]