const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.scheduledFunction = functions.pubsub
    .schedule("30 2 * * *")
    .timeZone("Asia/Kolkata").onRun(async (context) => {
      await admin.firestore().collection("functions").add({
        test: "executed",
      });
      console.log("New document added with data: test:executed");
      return null;
    });

