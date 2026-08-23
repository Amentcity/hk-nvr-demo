# Database Design

## Tables

### sys_user

- id
- username
- password
- role_id

### nvr_device

- id
- name
- ip
- port

### camera

- id
- device_id
- channel
- name
- rtsp_url

### stream_task

- id
- camera_id
- status

### operation_log

- id
- user_id
- action
- create_time
