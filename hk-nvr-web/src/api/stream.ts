import request from './request'

export function startStream(cameraId:number){
  return request({url:`/api/stream/start/${cameraId}`, method:'POST'})
}

export function stopStream(sessionId:string){
  return request({url:'/api/stream/stop', method:'POST', data:{sessionId}})
}

export function streamStatus(){
  return request({url:'/api/stream/status', method:'GET'})
}
