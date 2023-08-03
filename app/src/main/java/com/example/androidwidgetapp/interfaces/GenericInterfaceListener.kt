package com.example.androidwidgetapp.interfaces

interface GenericInterfaceListener<T> {
    fun clickListener(any: T)

    fun longClickListener(any: T)
}