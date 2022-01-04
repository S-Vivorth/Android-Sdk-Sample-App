package com.example.bill24sk

class itemModel {
    lateinit var imageString:String
    lateinit var bankName:String
    lateinit var fee:String

     constructor(imageString:String, bankName:String, fee:String){
         this.imageString = imageString
         this.bankName = bankName
         this.fee = fee
     }
    fun getImage():String{
        return imageString
    }
    fun getbankName():String{
        return bankName
    }
    fun getfee():String{
        return fee
    }
}