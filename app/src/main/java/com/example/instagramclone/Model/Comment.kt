package com.example.instagramclone.Model

class Comment {

    private var comment=""
    private var publisher=""

    constructor()
    constructor(comment: String, publisher: String) {
        this.comment = comment
        this.publisher = publisher
    }

    fun getComment():String
    {
        return comment
    }

    fun getPublisher():String
    {
        return publisher
    }


    fun setComment(comment:String)
    {
        this.comment=comment
    }

    fun setPublisher(publisher:String)
    {
        this.publisher=publisher
    }

}