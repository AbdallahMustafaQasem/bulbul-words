# Play Console - Ready-to-copy answers (Bulbul Words / بلبل الكلمات, com.amq.bulbulwords)

Verify each answer against the final build before submitting (especially the manifest permissions and dependencies).

## 1. Data safety form

**Data collection and security**
- Does your app collect or share any of the required user data types? **No**
  (All data is processed and stored only on the device and never transmitted off the device, so it is not "collected" under Google's definition. The child's first name and progress are stored locally in SharedPreferences only.)
- Is all of the user data collected by your app encrypted in transit? Not applicable (no data is transmitted; the app has no internet permission).
- Do you provide a way for users to request that their data be deleted? Not applicable (no data collected). Optional note: users can clear app data or uninstall to delete all local data.

**Data types (every category): select "not collected" for all**
- Location (approximate, precise): Not collected
- Personal info (name, email, user IDs, address, phone, race/ethnicity, political/religious beliefs, sexual orientation, other): Not collected
- Financial info (payment info, purchase history, credit score, other): Not collected
- Health and fitness (health info, fitness info): Not collected
- Messages (emails, SMS/MMS, other in-app messages): Not collected
- Photos and videos: Not collected
- Audio files (voice or sound recordings, music files, other): Not collected
- Files and docs: Not collected
- Calendar: Not collected
- Contacts: Not collected
- App activity (app interactions, in-app search history, installed apps, other user-generated content, other actions): Not collected (progress is kept on-device only)
- Web browsing: Not collected
- App info and performance (crash logs, diagnostics, other): Not collected
- Device or other IDs: Not collected

**Security practices**
- Data encrypted in transit: N/A (no data leaves the device)
- Users can request data deletion: N/A
- Committed to follow the Play Families Policy: **Yes**
- Independent security review: No

**Privacy policy URL:** (host the HTML pages and paste the public URL here; see checklist below)

## 2. Content rating questionnaire (IARC)

- Category: **Reference, News, or Educational** (educational app for children)
- Violence (any depiction, cartoon, fantasy, realistic, to humans or animals): **No**
- Sexuality / nudity: **No**
- Profanity / crude humor: **No**
- Controlled substances (drugs, alcohol, tobacco; references or use): **No**
- Gambling / simulated gambling: **No**
- Fear / horror themes: **No**
- User-generated content / users can interact or exchange content: **No**
- Sharing of user's location with other users: **No**
- Users can purchase digital goods: **No**
- App allows unrestricted web access / internet: **No**
- Shares personal information with third parties: **No**
- Expected result: IARC 3+ / PEGI 3 / ESRB Everyone / USK 0 (final rating is assigned by the questionnaire).

## 3. Target audience and content

- Target age groups: select **5 and under** and **6-8** (ages 2-5 are the core audience; 6-8 is included for children who need slower-paced practice).
- Does the app appeal to children? **Yes** (it is designed for them).
- Implications of targeting children (Families program):
  - The app must comply with the Google Play Families Policy and Designed for Families requirements; it will be shown to families and children.
  - No ads (nothing to certify, which makes this simple); if ads are ever added, only Families-certified ad SDKs may be used.
  - No personal data collection from children, no advertising ID, no unnecessary permissions (the app requests none).
  - The privacy policy must be linked and accurate; it covers children's privacy.
  - Content must be appropriate for children; the store listing must not be misleading and must not imply approval by Google or any therapy body.
  - Apps targeting children must not use sensitive APIs/permissions without need; the app uses none.
  - Because the app targets children under 13, the Play Console may ask for the "Designed for Families" opt-in and a data-practices review. Answer consistently with the Data safety form.
- Store listing note: avoid using words that appeal to adults only; the listing should describe the app honestly for parents.

## 4. Ads declaration
- Does your app contain ads? **No**

## 5. App access
- All functionality is available without special access. **No login, account, or special credentials are required.**
- Note for reviewers (optional): "The parent area (Settings tab and progress screen) is locked with a password. On first use, the app asks the parent to choose a password; to review it, open the Settings tab, choose any password (for example 1234), then use it to enter."

## 6. Other declarations

**Government apps:** Not a government app / not affiliated with any government. Answer: **No**.

**News apps:** Not a news app. Answer: **No**.

**Health apps declaration (Health Connect / health features):**
- Does the app have health features (medical, health and fitness)? Select **No health features**. It is an educational vocabulary and speech-practice app. It does not diagnose, treat, monitor or measure any condition, does not read any health data and is **not a medical device**.
- If the form asks about a medical-device or clinical function: **No**.
- Suggested disclaimer (use in the full description and, optionally, inside the app):
  - English: "Bulbul Words is an educational practice tool. It supports learning at home and can complement the work of speech-language therapists and teachers, but it does not diagnose, treat or replace professional therapy or advice. If you have concerns about your child's speech or development, please consult a qualified specialist."
  - Arabic: "«بلبل الكلمات» أداة تعليمية للتدريب. يدعم التعلّم في البيت ويمكن أن يكمّل عمل أخصائيي النطق واللغة والمعلّمين، لكنه لا يشخّص ولا يعالج ولا يغني عن العلاج أو الاستشارة المتخصصة. إذا كانت لديك مخاوف بشأن كلام طفلك أو نموّه فيُرجى استشارة مختص مؤهل."

**Other common declarations**
- Financial features: None.
- COVID-19 contact tracing/status: No.
- Advertising ID: **Not used** (declare "No"; the manifest should not contain the AD_ID permission - confirm in the merged manifest).
- Data deletion for accounts: Not applicable (no accounts).
- Content guidelines: no user-generated content.
- Export compliance / US export laws: standard (no custom encryption).
- Contact details: abdallahmqasem@gmail.com (public developer email).

## 7. Pre-launch checklist

**Store assets and sizes**
- App icon: 512 x 512 px, 32-bit PNG, max 1024 KB (no transparency tricks beyond what Play allows; keep full-bleed square, Play applies the mask).
- Feature graphic: 1024 x 500 px, JPEG or 24-bit PNG (no alpha).
- Phone screenshots: **at least 2** (recommended 8); JPEG or 24-bit PNG; each side 320-3840 px, longest side no more than twice the shortest. Recommended 1080 x 1920 portrait. See the screenshot lists in listing_en.md and listing_ar.md.
- Tablet 7" and 10" screenshots: optional but recommended if the app supports tablets.
- Short description and full description in Arabic and English (see listing files); set Arabic as default language or English, then add the other as a translation.
- App category: Education; tags as suggested in the listing files.

**Policy and legal**
- Privacy policy URL: publicly reachable, non-PDF, not behind login. Host privacy_policy_ar.html and privacy_policy_en.html with GitHub Pages: create a public repo (e.g. bulbul-words-privacy), put the files in the root or /docs, enable Settings > Pages, and use `https://<user>.github.io/<repo>/privacy_policy_en.html` (and the _ar page for the Arabic listing). Alternatives: Netlify, Cloudflare Pages, Firebase Hosting.
- Complete the Data safety, Content rating, Target audience, Ads, App access and other declarations above.
- Confirm no unused permissions in the merged AndroidManifest (the app should declare none; check that libraries do not add INTERNET or AD_ID).

**Build and signing**
- Build a signed Android App Bundle (.aab), release build, minify/shrink as configured.
- Use Play App Signing; keep the **upload key** (keystore + passwords) safely backed up in at least two places. Losing it complicates updates.
- versionCode 1 / versionName 1.0.0 for first release; target the current required targetSdk level.
- Test on at least one real device with Arabic and English TTS voices installed, and on a device with no Arabic voice installed (verify graceful behavior).

**Testing requirement (new personal developer accounts)**
- New personal developer accounts created after November 13, 2023 must run a **closed test with at least 12 testers opted in continuously for 14 days** before applying for production access. Recruit testers (family, friends, parents' groups) early, create a closed testing track, add testers by email list or Google Group, then apply for production in Play Console after 14 days. Confirm the current numbers in Play Console, as requirements can change.
- Organization accounts are not subject to this requirement.

**Final checks**
- Pre-launch report in Play Console reviewed for crashes.
- Developer contact email set to abdallahmqasem@gmail.com on the store listing.
- Release notes (What's new) for 1.0.0 pasted in each language.
- Roll out to production in stages if desired.

## Links to Google Play and other apps (Families policy)
- The "About the developer" screen (rate this app, contact email, and the developer's other apps) is inside the parent area, behind the parent's password. Children cannot reach any link that leaves the app.
- The links open the Google Play store (market:// or https://play.google.com) and an email composer. There are no ads and no web view.
- The five apps listed are the ones live on Google Play (status "Production") at the time of writing: Utilix, Science Lab, Phone Checkup, Currency Exchange and the Quran app. Update the list in `Developer.kt` when the live apps change.
