# 🏦 Banking Demo Backend

This repository hosts the source code for the backend of our demo banking application.

## 🐋 Docker Setup

To build the docker image, simply build the dockerfile present in the root directory.

```bash
$ docker buildx build -t banking-backend .
$ docker run -p 5005:5005 banking-backend
```

