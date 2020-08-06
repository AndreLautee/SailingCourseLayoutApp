package com.example.sailinglayoutapp;

import android.location.Location;
import android.location.LocationManager;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

public class MarkerCoordCalculationsTest {

    @Mock
    Location mockLocation1 = new Location(LocationManager.GPS_PROVIDER);
    @Mock
    Location mockLocation2 = new Location(LocationManager.GPS_PROVIDER);
    @Mock
    Location mockLocation3 = new Location(LocationManager.GPS_PROVIDER);


    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void getCoordsCalculatesCorrectMarks() {

        // Create first mock location as reference mark
        when(mockLocation1.getLatitude()).thenReturn(100.00);
        when(mockLocation1.getLongitude()).thenReturn(-50.00);

        CourseVariables cv = new CourseVariables("triangle", "N", 5, 60,
                "starboard", null);
        MarkerCoordCalculations mcc = new MarkerCoordCalculations(mockLocation1, cv);

        // Create second mock location to test
        when(mockLocation2.getLatitude()).thenReturn(mcc.testGetCoords(10.00,-50.00,112,Math.PI).get(0));
        when(mockLocation2.getLongitude()).thenReturn(mcc.testGetCoords(10.00,50.00,112,Math.PI/2).get(1));

        // Manually add arraylist to mcc.coords because
        // returned locations in mcc are not mocked therefore
        // lat and long are set to default
        ArrayList<Location> locationArrayList = new ArrayList<>();
        locationArrayList.add(mockLocation1);
        locationArrayList.add(mockLocation2);
        mcc.setCoords(locationArrayList);

        assertEquals(51, mcc.getCoords().get(1).getLongitude(),1);
    }

    @Test
    public void createTriangleReturnsCorrectCoordinates() {

        when(mockLocation1.getLatitude()).thenReturn(100.00);
        when(mockLocation1.getLongitude()).thenReturn(-50.00);

        CourseVariables cv = new CourseVariables("triangle", "N", 5, 60,
                "starboard", null);
        MarkerCoordCalculations mcc = new MarkerCoordCalculations(mockLocation1,cv);
        //mcc.createShape("triangle");
        assertEquals(100.00, mcc.getCoords().get(0).getLatitude(),0);
    }


    @Test
    public void getCorrectBearings() {
        CourseVariables cv = new CourseVariables();

        assertEquals(Math.PI/4,cv.getBearingsInRadians("NE"),0);
    }

}
