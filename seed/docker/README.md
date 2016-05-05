Using Docker to build Ontrack
=============================

This Docker image is using on Jenkins to build Ontrack but can be used also to build it locally, using your
 own Docker installation.

Go to your working copy of Ontrack and start by building the Ontrack build image:
 
```bash
docker build -t "nemerosa/ontrack-build" seed/docker
```

When done, run the build in this image:

```bash
docker run \
    --volume $HOME/.gradle:/root/.gradle \
    --volume $HOME/.cache:/root/.cache \
    --volume `pwd`:/workspace \
    nemerosa/ontrack-build \
    /workspace/gradlew --info --stacktrace --profile --project-dir /workspace clean build
```

> Note that this can be really slow if you're running your Docker on a virtual machine. In native mode, like
> in a Linux box, it's much faster :)
