import request from './request'

export function getCameraList(){
 return request({url:'/api/camera/list',method:'GET'})
}

export function updateCamera(data:any){
 return request({url:'/api/camera',method:'PUT',data})
}
