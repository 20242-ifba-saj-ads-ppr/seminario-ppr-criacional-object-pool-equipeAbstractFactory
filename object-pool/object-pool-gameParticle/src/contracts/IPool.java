package contracts;

public interface IPool<T>{
  T acquire();
  void release(T t);
}

  
