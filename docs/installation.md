# Installation instructions for Podium

## Prepare database

Run `sudo -u postgres psql` and execute these commands to create the
required databases and users:

```sql
create database "podiumUaa";
create database "podiumGateway";
create role "podiumUaa" with password '<choose a secret>';
create role "podiumGateway" with password '<choose another secret>';
grant all on database "podiumUaa" to "podiumUaa";
grant all on database "podiumGateway" to "podiumGateway";
```
Use a password manager to generate strong random passwords, or, e.g.,:
```commandline
openssl rand -base64 32
```

## Files

### Create podium system user

```bash
sudo adduser --disabled-login podium
```

### Download application archives

```commandline
sudo -iu podium
REPO=https://repo.thehyve.nl/service/local/artifact/maven/redirect?r=releases
REGISTRY_VERSION=1.0.4
PODIUM_VERSION=1.0.0
curl -L "${REPO}&g=nl.thehyve.podium&a=podium-registry&v=${REGISTRY_VERSION}&p=war" -o podium-registry.war
curl -L "${REPO}&g=nl.thehyve.podium&a=podium-uaa&v=${PODIUM_VERSION}&p=war" -o podium-uaa.war
curl -L "${REPO}&g=nl.thehyve.podium&a=podium-gateway&v=${PODIUM_VERSION}&p=war" -o podium-gateway.war
```

### Startup scripts

Place the following startup scripts in the home directory `/home/podium`:

- `/home/podium/start_registry`:
    ```bash
    #!/usr/bin/env bash
    
    cd "$( dirname "${BASH_SOURCE[0]}" )"
    java -jar -server -Djava.awt.headless=true -Xms200m -Xmx200m -Dspring.profiles.active=prod -Djava.security.egd=file:///dev/urandom -Dspring.config.location=/home/podium/registry-config.yml /home/podium/podium-registry.war
    ```
- `/home/podium/start_uaa`:
    ```bash
    #!/usr/bin/env bash
    
    cd "$( dirname "${BASH_SOURCE[0]}" )"
    java -jar -server -Djava.awt.headless=true -Xms1g -Xmx1g  -Dspring.profiles.active=prod -Djava.security.egd=file:///dev/urandom -Dspring.config.location=/home/podium/uaa-config.yml /home/podium/podium-uaa.war
    ```
- `/home/podium/start_gateway`:
    ```bash
    #!/usr/bin/env bash
    
    cd "$( dirname "${BASH_SOURCE[0]}" )"
    java -jar -server -Djava.awt.headless=true -Xms2g -Xmx2g -Dspring.profiles.active=prod -Djava.security.egd=file:///dev/urandom -Dserver.port=8082 -Dspring.config.location=/home/podium/gateway-config.yml /home/podium/podium-gateway.war
    ```

Make the scripts executable:
```bash
chmod +x start_*
```

### Configure the registry repository

The preferred way to serve a configuration repository to the registry is _using a local directory_;

The structure of the repository has to be as in the [example configuration repository](https://github.com/thehyve/podium-example-config).
The location can be specified in the configuration file of the registry, see `registry-config.yml` below.


### Config files
- `/home/podium/registry-config.yml`:
    ```yaml
    cloud:
        config:
            server:
                native:
                    search-locations: file:./central-config
    podium:
        security:
            authentication:
                jwt:
                    secret: change-me-in-production
    security:
        user:
            password: change-admin-password-in-production
    ```
- `/home/podium/uaa-config.yml`:
    ```yaml
    spring:
        datasource:
            url: jdbc:postgresql://localhost:5432/podiumUaa
            username: podiumUaa
            password: <choose a secure password>
    podium:
        mail:
            from: podium@example.com
            baseUrl: https://example.com
        registry:
            password: change-admin-password-in-production

    ```
- `/home/podium/gateway-config.yml`:<br>
   Here also the token can be configured for the [Molgenis Biobank Directory] integration,
   in the `podium.access.request-template` property.
    ```yaml
    spring:
        datasource:
            url: jdbc:postgresql://localhost:5432/podiumGateway
            username: podiumGateway
            password: <choose another secure password>
    podium:
        mail:
            from: podium@example.com
            baseUrl: https://example.com
        registry:
            password: change-admin-password-in-production
        access:
            request-template: # Configure Basic Authentication for the /api/public/requests/template endpoint.
                - <username>:<password>
    ```


### Systemd services

We use `systemd` to define and control the different services of Podium.

- `/etc/systemd/system/podium-registry.service`:
    ```ini
    [Unit]
    Description=Podium Registry microservice
    
    [Service]
    User=podium
    WorkingDirectory=/home/podium
    ExecStart=/home/podium/start_registry
    StandardOutput=journal+console
    
    [Install]
    WantedBy=multi-user.target
    ```
- `/etc/systemd/system/podium-uaa.service`:
    ```ini
    [Unit]
    Description=Podium User Authentication microservice
    After=podium-registry.service
    
    [Service]
    User=podium
    WorkingDirectory=/home/podium
    ExecStartPre=/bin/sleep 10
    ExecStart=/home/podium/start_uaa
    StandardOutput=journal+console
    
    [Install]
    WantedBy=multi-user.target
    ```
- `/etc/systemd/system/podium-gateway.service`:
    ```ini
    [Unit]
    Description=Podium Gateway microservice
    After=podium-registry.service podium-uaa.service
    
    [Service]
    User=podium
    WorkingDirectory=/home/podium
    ExecStartPre=/bin/sleep 20
    ExecStart=/home/podium/start_gateway
    StandardOutput=journal+console
    
    [Install]
    WantedBy=multi-user.target
    ```
    
### Starting the services

The services can be started with the following commands:
```bash
sudo systemctl start podium-registry.service
sudo systemctl start podium-uaa.service
sudo systemctl start podium-gateway.service
```

### Status and logging

The status of the services can be checked with `systemctl status`:
```bash
systemctl status podium-registry.service
systemctl status podium-uaa.service
systemctl status podium-gateway.service
```
The logs can be inspected with `journalctl`:
```bash
sudo journalctl -u podium-registry.service -f
sudo journalctl -u podium-uaa.service -f
sudo journalctl -u podium-gateway.service -f
```

Log files are also written to the `/home/podium/logs` directory.

The status of the services can also be checked using the web interface
of the registry at http://localhost:8761.
This port should not be accessible from outside. To view this page,
you can forward the port via ssh:
```bash
ssh -L 8761:localhost:8761 ${servername}
```

## Create first admin user

Register as regular podium user and upgrade the user using `sudo -u postgres psql -d podiumUaa`:
```sql
# Assign Podium admin role
insert into role_users (users_id, roles_id)
select u.id, r.id from podium_user u, podium_role r where u.login = '<login>' and r.authority_name = 'ROLE_PODIUM_ADMIN';
# Verify account
update podium_user set admin_verified = true where login = '<login>';
```

[Molgenis Biobank Directory]: https://molgenis.gitbooks.io/molgenis/content/user_documentation/catalogues/biobank-directory.html
