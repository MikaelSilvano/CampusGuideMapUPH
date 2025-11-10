const path = require('path');
const admin = require('firebase-admin');

//const keyPath = path.join(__dirname, 'keys', 'campus-guide-map-uph-firebase-adminsdk-fbsvc-31174c1e26.json');
const identifier = process.argv[2];

if (!identifier) {
  console.error('Usage: node grant-admin.js <UID_or_EMAIL>');
  process.exit(1);
}
//
//const serviceAccount = require(keyPath);
//admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });

(async () => {
  let uid = identifier;
  if (identifier.includes('@')) {
    const user = await admin.auth().getUserByEmail(identifier);
    uid = user.uid;
  } else {
    await admin.auth().getUser(identifier);
  }
  await admin.auth().setCustomUserClaims(uid, { role: 'admin' });
  console.log('OK set role=admin for', uid);
})();
