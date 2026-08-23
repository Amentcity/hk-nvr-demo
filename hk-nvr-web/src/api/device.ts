import request from './request'

export function getDeviceList(){
 return request({url:'/api/device/list',method:'GET'})
}

export function addDevice(data:any){
 return request({url:'/api/device',method:'POST',data})
}

export function deleteDevice(id:number){
 return request({url:`/api/device/${id}`,method:'DELETE'})
}
