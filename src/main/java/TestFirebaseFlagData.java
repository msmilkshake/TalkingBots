import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TestFirebaseFlagData {
    
    private FirebaseOptions options;
    private final FirebaseDatabase database;
    private DatabaseReference ref;
    
    private final Object lock;
    
    public TestFirebaseFlagData() {
        try {
            FileInputStream serviceAccount = new FileInputStream("pvt/admin.json");
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://startbots-81ecb.firebaseio.com/")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        FirebaseApp.initializeApp(options);
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("flag");
        
        lock = new Object();
    }
    
    public void start() {
        long startTime = System.currentTimeMillis();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("inside dataChange");
                int s = dataSnapshot.child("value").getValue(Integer.class);
                System.out.println(s);
                
                // Notify main execution
                synchronized (TestFirebaseFlagData.this) {
                    TestFirebaseFlagData.this.notify();
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Data changed.");
            }
        
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        
        // Wait until database gets read
        synchronized (this) {
            try {
                wait();
                Thread.sleep(30000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Took " + (endTime - startTime) + " ms.");
        System.out.println("At the end");
    }
}
