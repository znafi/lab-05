package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        // Add snapshot listener to sync data from Firestore
        citiesRef.addSnapshotListener((querySnapshots, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error listening to changes", error);
                return;
            }
            if (querySnapshots != null) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot doc : querySnapshots) {
                    String name = doc.getString("name");
                    String province = doc.getString("province");
                    if (name != null && province != null) {
                        cityArrayList.add(new City(name, province));
                    }
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });

    }

    @Override
    public void updateCity(City city, String title, String year) {
        String oldName = city.getName();
        
        // Delete old document
        citiesRef.document(oldName)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Old city deleted successfully");
                    
                    // Add new document with updated data
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", title);
                    data.put("province", year);
                    
                    citiesRef.document(title)
                            .set(data)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("Firestore", "City updated successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error updating city", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting old city", e);
                });
    }

    @Override
    public void addCity(City city){
        Map<String, Object> data = new HashMap<>();
        data.put("name", city.getName());
        data.put("province", city.getProvince());
        
        citiesRef.document(city.getName())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "DocumentSnapshot successfully written!");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding city", e);
                });
    }

    public void deleteCity(City city) {
        citiesRef.document(city.getName())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "City deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting city", e);
                });
    }
}