import request from './request'

export function getUserList(){
 return request({url:'/api/user/list',method:'GET'})
}

export function createUser(data:any){
 return request({url:'/api/user',method:'POST',data})
}
