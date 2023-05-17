package com.shop.exception;



public class OutOfStockException extends RuntimeException
{
    public OutOfStockException(String message)
    {
        // RuntimeException에 메세지를 전달해야한다!
        super(message);
    }
}
