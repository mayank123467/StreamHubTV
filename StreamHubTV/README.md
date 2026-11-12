# StreamHub TV (Android TV Aggregator)

A Jetpack Compose for TV hub for **ZEE5, SonyLIV, Prime Video, YouTube, JioHotstar, and JioTV**.

## Build Your APK Automatically (No Coding Needed)
1. Create a free account on GitHub and create a new repository.
2. Upload all files inside this folder (make sure `.github/workflows/build-apk.yml` is included).
3. Add your free API keys under **Settings -> Secrets and variables -> Actions**:
   - `TMDB_API_KEY` (from themoviedb.org)
   - `YOUTUBE_API_KEY` (from Google Cloud Console -> YouTube Data API v3)
4. Open the **Actions** tab -> **Build Android TV APK** -> **Run workflow**.
5. Download `StreamHubTV-APK.zip`, extract `app-debug.apk`, and install it on your Android TV.
