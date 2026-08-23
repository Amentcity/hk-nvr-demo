import request from './request'

export function searchRecord(params:any){
  return request({url:'/api/record/search', method:'GET', params})
}

export function startPlayback(data:any){
  return request({url:'/api/playback/start', method:'POST', data})
}

export function stopPlayback(taskId:string){
  return request({url:'/api/playback/stop', method:'POST', data:{taskId}})
}
