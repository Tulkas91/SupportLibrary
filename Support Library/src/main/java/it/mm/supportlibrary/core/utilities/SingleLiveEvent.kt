package it.mm.supportlibrary.core.utilities

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val observers = mutableSetOf<Observer<in T>>()

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val wrapper = Observer<T> { t ->
            if (observers.remove(observer)) {
                observer.onChanged(t)
            }
        }
        observers.add(observer)
        super.observe(owner, wrapper)
    }

    fun emit(value: T) {
        observers.forEach { _ -> /* keep them */ }
        postValue(value)
    }
}