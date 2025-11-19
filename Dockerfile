FROM ubuntu:latest
LABEL authors="Pedro"

ENTRYPOINT ["top", "-b"]