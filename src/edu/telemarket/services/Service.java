package edu.telemarket.services;

import edu.telemarket.https.Request;
import edu.telemarket.https.Response;

/**
 * Be careful!
 * Created by hason on 15/9/18.
 */
public interface Service {

    Response execute(Request request);
}
