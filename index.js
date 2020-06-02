const functions = require('firebase-functions');
const admin = require('firebase-admin');

var serviceAccount = require("./permissions.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://startbots-81ecb.firebaseio.com"
});

const express = require('express');
const app = express();
const db = admin.firestore();
const rtdb = admin.database();

const cors = require('cors');
app.use(cors({ origin: true }));



//Routes **NOT USED**
app.get('/hello-world', (req, res) => {
    return res.status(200).send('Hello, world!');
});

//Create
//Post
//Firestore **NOT USED**
app.post('/api/create', (req, res) => {

    (async () => {

        try {
            await db.collection('products').doc('/' + req.body.id + '/')
                .create({
                    name: req.body.name,
                    description: req.body.description,
                    price: req.body.price
                })

            return res.status(200).send();
        } catch (error) {
            console.log(error);
            return res.status(500).send(error);
        } s

    })();
});

//Realtime **NOT USED**
app.post('/api/rt/create', (req, res) => {

    (async () => {

        try {
            let ref = rtdb.ref("products");
            let idRef = ref.child(req.body.id);
            idRef.set({
                name: req.body.name,
                description: req.body.description,
                price: req.body.price
            })

            return res.status(200).send();
        } catch (error) {
            console.log(error);
            return res.status(500).send(error);
        }

    })();
});

//Read
//Get
//Firestore
//Get Product by ID **NOT USED**
app.get('/api/read/:id', (req, res) => {

    (async () => {

        try {
            const document = db.collection('products').doc(req.params.id);
            let product = await document.get();
            let response = product.data();

            return res.status(200).send(response);
        } catch (error) {
            console.log(error);
            return res.status(500).send(error);
        } s

    })();
});


//Realtime
//Get Product by ID **NOT USED**
app.get('/api/rt/read/:id', (req, res) => {

    (async () => {

        try {
            let ref = rtdb.ref("products/" + req.params.id);
            ref.on('value', function (snapshot) {
                console.log(snapshot.val());

                return res.status(200).send(snapshot.val());
            }, function (errorObject) {
                console.log("The read failed: " + errorObject.code);
                throw (error);
            });


        } catch (error) {
            console.log(error);
            return res.status(500).send(error);
        }

    })();
});

//Update
//Put

//Delete
//Delete


//Dialogflow Request
//Default response **NOT USED**
app.post('/api/bot/default', async (req, res) => {
    await new Promise(r => setTimeout(r, 2000));
    res.status(200).json({
        fulfillmentText: "This message is the default response message."
    })
})

//Just a test **NOT USED**
app.post('/api/bot/getfromdb', (req, res) => {
    let path = rtdb.ref("test");
    path.on('value', function (snapshot) {

        return res.status(200).send(snapshot.val());

    }, function (errorObject) {

        console.log("The read failed: " + errorObject.code);
        return res.status(500).send();

    });
});

//Trigger Bot Chat TEST **NOT USED**
app.post('/api/bot/triggerchattest', (req, res) => {
    let flag = rtdb.ref("flag");
    let msg = rtdb.ref("msg");
    msg.on('value', function (snapshot) {

        let tts = snapshot.val();
        (async () => {

            try {

                flag.set({
                    value: 1
                })

            } catch (error) {
                console.log(error);
            }

        })();

        let afterMsg = snapshot.child("fulfillmentText").val();

        res.status(200).send(tts);
        msg.set({
            fulfillmentText: afterMsg,
            testing: "This is a testoo"
        });

        return;

    }, function (errorObject) {

        console.log("The read failed: " + errorObject.code);
        return res.status(500).send();

    });
})

//Trigger Bot 1 Chat ~REPLACED~ **NOT USED**
app.post('/api/bot/trigger1', async (req, res) => {
    let flag = rtdb.ref("flag");
    let msg = rtdb.ref("msg1");
    let justSaid1 = rtdb.ref("justSaid1")

    msg.once('value', async function (snapshot) {

        await new Promise(r => setTimeout(r, 350));
        
        let saidMessage = String(snapshot.child("fulfillmentText").val());

        //if (saidMessage.includes("Short response: ")) {
        //    await new Promise(r => setTimeout(r, 250));
        //}

        saidMessage = saidMessage
                .replace(" b b y y e", "bye")
                .replace("s t t o p", "stop")
                .replace("m i i n d", "mind")
                .replace("c l l o s e e", "close")
                .replace(" *laughting out loud* ", "lol");

        justSaid1.set(saidMessage);

        let response = snapshot.val();

        res.status(200).send(response);

        let bufferMsg = snapshot.child("bufferMsg").val();

        msg.set({
            fulfillmentText: bufferMsg,
            bufferMsg: bufferMsg
        });

        flag.set({
            value: 1
        });

        return;

    }, function (errorObject) {

        console.log("The read failed: " + errorObject.code);
        return res.status(500).send();

    });
});

//Trigger Bot 1 Chat
app.post('/api/bot1', async (req, res) => {
    let flag = rtdb.ref("flag");
    let msg = rtdb.ref("msg1");
    let justSaid1 = rtdb.ref("justSaid1");

    await new Promise(r => setTimeout(r, 1500));

    //console.log(req);
    //console.log(req.body);
    //console.log(req.body.queryResult.queryText);
    //if (String(req.body.queryResult.queryText).includes("actions_intent_NO_INPUT")) {
    //    console.log("No Input Detected!")
    //}

    msg.once('value', function  (snapshot) {

        let saidMessage = String(snapshot.child("fulfillmentText").val());

        saidMessage = saidMessage
                .replace(" b b y y e", "bye")
                .replace("s t t o p", "stop")
                .replace("m i i n d", "mind")
                .replace("c l l o s e e", "close")
                .replace(" *laughting out loud* ", "lol");

        justSaid1.set(saidMessage)
        .then(sp => {

            let response = snapshot.val();
            let bufferMsg = snapshot.child("bufferMsg").val();

            msg.set({
                fulfillmentText: bufferMsg,
                bufferMsg: bufferMsg
            })
            .then(sp => {

                flag.set({
                    value: 1
                });

                return res.status(200).send(response);
            })
            .catch(error => {
                console.log(error);
                return res.status(500).send(error);
            });
        })
        .catch(error => {
            console.log(error);
            return res.status(500).send(error);
        });

    }, function (error) {

        console.log("The read failed: " + error.code);
        return res.status(500).send(error);

    });
});

//Trigger Bot 2 Chat
app.post('/api/bot2', async (req, res) => {
    let flag = rtdb.ref("flag");
    let msg = rtdb.ref("msg2");
    let justSaid2 = rtdb.ref("justSaid2");

    await new Promise(r => setTimeout(r, 1500));

    //console.log(req);
    //console.log(req.body);
    //console.log(req.body.queryResult.queryText);
    //if (String(req.body.queryResult.queryText).includes("actions_intent_NO_INPUT")) {
    //    console.log("No Input Detected!")
    //}

    msg.once('value', function  (snapshot) {

        let saidMessage = String(snapshot.child("fulfillmentText").val());

        saidMessage = saidMessage
                .replace(" b b y y e", "bye")
                .replace("s t t o p", "stop")
                .replace("m i i n d", "mind")
                .replace("c l l o s e e", "close")
                .replace(" *laughting out loud* ", "lol");

        justSaid2.set(saidMessage)
        .then(sp => {

            let response = snapshot.val();
            let bufferMsg = snapshot.child("bufferMsg").val();

            msg.set({
                fulfillmentText: bufferMsg,
                bufferMsg: bufferMsg
            })
            .then(sp => {

                flag.set({
                    value: 2
                });

                return res.status(200).send(response);
            })
            .catch(error => {
                console.log(error);
                return res.status(500).send(error);
            });
        })
        .catch(error => {
            console.log(error);
            return res.status(500).send(error);
        });

    }, function (error) {

        console.log("The read failed: " + error.code);
        return res.status(500).send(error);

    });
});

//Export the api to Firebase Cloud Functions    
exports.app = functions.region('europe-west3').https.onRequest(app);