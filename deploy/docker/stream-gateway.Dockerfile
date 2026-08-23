FROM ubuntu:22.04

RUN apt update && apt install -y ffmpeg

WORKDIR /stream

EXPOSE 8081

CMD ["ffmpeg","-version"]
