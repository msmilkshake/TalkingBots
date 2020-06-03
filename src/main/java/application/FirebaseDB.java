package application;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;

public class FirebaseDB {
    
    private FirebaseOptions options;
    private final FirebaseDatabase DATABASE;
    private final DatabaseReference REF;
    private final DatabaseReference JUST_SAID_1;
    private final DatabaseReference JUST_SAID_2;
    
    private ChatWindow chat;
    
    private int requestNumber;
    
    private boolean isListening;
    
    public FirebaseDB(String filename, String dbUrl) {
        try {
            FileInputStream serviceAccount = new FileInputStream("pvt/" + filename);
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(dbUrl)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        isListening = false;
        
        //FirebaseApp.initializeApp(options, String.valueOf(hashCode()));
        FirebaseApp fbApp = FirebaseApp.initializeApp(options, String.valueOf(hashCode()));
        DATABASE = FirebaseDatabase.getInstance(fbApp);
        REF = DATABASE.getReference("flag");
        JUST_SAID_1 = DATABASE.getReference("justSaid1");
        JUST_SAID_2 = DATABASE.getReference("justSaid2");
        
        requestNumber = 0;
        createChatThread();
        
        createListeners();
        
    }
    
    private void createChatThread() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                chat = new ChatWindow();
            }
        });
        t.start();
    }
    
    private void createListeners() {
    
        REF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                
                int value = dataSnapshot.child("value").getValue(Integer.class);
                //System.out.println("Bot changed flag value to " + value);
                requestNumber = value;
            }
        
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        
        JUST_SAID_1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isListening && chat.getInitial().equals("M")) {
                    String msg = dataSnapshot.getValue(String.class);
                    chat.printMessageInConsole(msg);
                }
            }
    
            @Override
            public void onCancelled(DatabaseError databaseError) {
        
            }
        });
    
        JUST_SAID_2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isListening && chat.getInitial().equals("F")) {
                    String msg = dataSnapshot.getValue(String.class);
                    chat.printMessageInConsole(msg);
                }
            }
        
            @Override
            public void onCancelled(DatabaseError databaseError) {
            
            }
        });
        
    }
    
    public void setListening(boolean val) {
        isListening = val;
    }
    
    public void readValue() {
        REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("inside dataChange");
                int s = dataSnapshot.child("value").getValue(Integer.class);
                System.out.println(s);
            }
        
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }
    
    public void start() {
        long startTime = System.currentTimeMillis();
        REF.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("inside dataChange");
                int s = dataSnapshot.child("value").getValue(Integer.class);
                System.out.println(s);
                
                // Notify main execution
                synchronized (FirebaseDB.this) {
                    FirebaseDB.this.notify();
                }
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
    
    public int getRequestNumber() {
        return requestNumber;
    }
    
    public void disableFlag() {
        REF.child("value").setValue(0, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                synchronized (FirebaseDB.this) {
                    FirebaseDB.this.notify();
                }
            }
        });
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void putMessage(String ref, String message) {
        putMessage(ref, message, "fulfillmentText");
    }
    
    public void putMessage(String ref, String message, String child) {
        DatabaseReference dbRef = DATABASE.getReference(ref);
        dbRef.child(child).setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                synchronized (FirebaseDB.this) {
                    FirebaseDB.this.notify();
                }
            }
        });
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
