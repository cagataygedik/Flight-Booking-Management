package core;

import java.io.Serializable;

public interface BookingComponent extends Serializable {
    double getCost();
    String getDescription();
    Flight getFlight();
}