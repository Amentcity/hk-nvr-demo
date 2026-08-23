# Enterprise NVR Architecture

## Overview

Enterprise NVR platform architecture:

```text
Vue3 Web Client
      |
 API Gateway
      |
 NVR Service ---- HCNetSDK ---- Hikvision NVR
      |
 Stream Service ---- FFmpeg ---- WebRTC
      |
 Playback Service ---- Record Storage
```

## Video Flow

RTSP -> FFmpeg -> WebRTC -> Browser

## Modules

- Device Management
- Camera Management
- Stream Service
- Playback Service
- User and RBAC
- Operation Audit
