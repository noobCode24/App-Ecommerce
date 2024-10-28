package com.example.app_ecommerce.utils;

import com.example.app_ecommerce.Model.ShoppingCart;
import com.example.app_ecommerce.Model.User;

import java.util.List;

public class Utils {
    // url co so ket noi toi mien cua server chua cac file php
    public static final String BASE_URL = "http://192.168.1.12/app_Ecommerce/";
    public static List<ShoppingCart> ShoppingCartList;
    public static User user_current = new User();
}
