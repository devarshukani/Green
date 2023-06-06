const functions = require('firebase-functions');
const admin = require('firebase-admin');
const { Graph } = require('graphlib');
const { google } = require('googleapis');

admin.initializeApp();

const firestore = admin.firestore();

// Configure the Google Maps API client
const googleMapsClient = google.maps.createClient({
  key: 'YOUR_GOOGLE_MAPS_API_KEY',
  Promise: Promise,
});

exports.calculateOptimalRoute = functions.https.onRequest(async (req, res) => {
  try {
    // Retrieve user and collector location data from Firestore
    const usersSnapshot = await firestore.collection('userlocation').get();
    const collectorsSnapshot = await firestore.collection('collectorlocation').get();

    const users = [];
    const collectors = [];

    usersSnapshot.forEach((doc) => {
      const { latitude, longitude } = doc.data();
      users.push({ id: doc.id, latitude, longitude });
    });

    collectorsSnapshot.forEach((doc) => {
      const { latitude, longitude } = doc.data();
      collectors.push({ id: doc.id, latitude, longitude });
    });

    // Build a graph using user and collector locations
    const graph = new Graph();

    users.forEach((user) => {
      collectors.forEach(async (collector) => {
        const distance = await calculateDistance(user, collector); // Calculate distance using Google Maps Directions API
        graph.setEdge(user.id, collector.id, distance);
      });
    });

    // Find the best route for each collector using the nearest neighbor algorithm
    const optimalRoutes = {};

    collectors.forEach((collector) => {
      const path = findNearestNeighborPath(graph, collector.id, users);
      optimalRoutes[collector.id] = path;
    });

    // Store the result in a new collection in Firestore
    const resultCollection = firestore.collection('optimalRoutes');
    const batch = firestore.batch();

    Object.keys(optimalRoutes).forEach((collectorId) => {
      const routeData = {
        collectorId,
        path: optimalRoutes[collectorId],
      };

      const routeRef = resultCollection.doc(collectorId);
      batch.set(routeRef, routeData);
    });

    await batch.commit();

    res.status(200).send('Optimal routes calculated and stored successfully.');
  } catch (error) {
    console.error('Error calculating optimal routes:', error);
    res.status(500).send('Error calculating optimal routes.');
  }
});

async function calculateDistance(locationA, locationB) {
  const origin = `${locationA.latitude},${locationA.longitude}`;
  const destination = `${locationB.latitude},${locationB.longitude}`;

  const response = await googleMapsClient.directions({
    origin,
    destination,
  }).asPromise();

  const distance = response.data.routes[0].legs[0].distance.value;
  return distance;
}

function findNearestNeighborPath(graph, startNode, nodes) {
  const path = [startNode];
  const unvisitedNodes = [...nodes];
  let currentNode = startNode;

  while (unvisitedNodes.length > 0) {
    let nearestNeighbor = null;
    let nearestNeighborDistance = Infinity;

    unvisitedNodes.forEach((node) => {
      const distance = graph.edge(currentNode, node);
      if (distance < nearestNeighborDistance) {
        nearestNeighbor = node;
        nearestNeighborDistance = distance;
      }
    });

    path.push(nearestNeighbor);
    unvisitedNodes.splice(unvisitedNodes.indexOf(nearestNeighbor), 1);
    currentNode = nearestNeighbor;
  }

  return path;
}



// exports.scheduledFunction = functions.pubsub
//     .schedule("30 2 * * *")
//     .timeZone("Asia/Kolkata").onRun(async (context) => {
//       await admin.firestore().collection("functions").add({
//         test: "executed",
//       });
//       console.log("New document added with data: test:executed");
//       return null;
//     })