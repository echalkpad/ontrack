Ontrack pipeline
================

@startuml
database GitHub
control Build
database LocalDocker
control Acceptance
actor Owner
control DockerPush
database DockerHub
control DOAcceptance
entity Droplet
control DebAcceptance
control Centos6Acceptance
control Centos7Acceptance
control Publication
control Production
control ProductionAcceptance
entity Ontrack

GitHub -> Build: Triggers

== Build phase ==

group Docker slave
    Build -> Build: Unit tests and package
    Build -> LocalDocker: Latest image
    Build -> Acceptance: Triggers
    Acceptance -> LocalDocker: Get latest image
    Acceptance -> Acceptance: Local acceptance tests
    Acceptance -> Owner: Manual
    == Docker phase ==
    Owner -> DockerPush: Triggers
    DockerPush -> LocalDocker: Get latest image
    DockerPush -> DockerHub: Pushes and tags
end

== Acceptance phase ==

DockerPush -> DOAcceptance: Triggers
DOAcceptance -> Droplet: Create
DOAcceptance -> DockerHub: Gets latest image
DOAcceptance -> Droplet: Deploys
DOAcceptance -> Droplet: Acceptance tests

alt debian
    DOAcceptance -> DebAcceptance: Triggers
    DebAcceptance -> DockerHub: Gets latest image
    DebAcceptance -> DebAcceptance: Local tests
else centos6
    DOAcceptance -> Centos6Acceptance: Triggers
    Centos6Acceptance -> DockerHub: Gets latest image
    Centos6Acceptance -> Centos6Acceptance: Local tests
else centos7
    DOAcceptance -> Centos7Acceptance: Triggers
    Centos7Acceptance -> DockerHub: Gets latest image
    Centos7Acceptance -> Centos7Acceptance: Local tests
end

== Publication phase ==

Owner -> Publication: Triggers
Publication -> GitHub: Publication

== Production phase ==

Owner -> Production: Triggers
Production -> DockerHub: Gets latest image
Production -> Ontrack: Deploys
Production -> ProductionAcceptance: Triggers
ProductionAcceptance -> Ontrack: Smoke tests

@enduml
