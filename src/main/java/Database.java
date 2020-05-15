import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Database {
    public static void main(String[] args) {
        // Fetch the service account key JSON file contents
        FileInputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream("pvt/admin1.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

// Initialize the app with a service account, granting admin privileges
        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://startbots-81ecb.firebaseio.com/")
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FirebaseApp.initializeApp(options);

// As an admin, the app has access to read and write all data, regardless of Security Rules
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("restricted_access/secret_document");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object document = dataSnapshot.getValue();
                System.out.println(document);
            }
        
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}
