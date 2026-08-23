import request from './request'

export function getDevices(){
 return request({url:'/api/device/list',method:'GET'})
}

export function addDevice(data:any){
 return request({url:'/api/device',method:'POST',data})
}
