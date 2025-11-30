# Movie App for Android TV

A completely redesigned Android TV movie browsing application with excellent D-pad navigation, modern UI, and support for both Movies and TV Series.

## Features

### Core Functionality
- **Browse Movies**: View movies in multiple categories (Now Playing, Popular, Top Rated, Upcoming).
- **Browse Series**: Dedicated section for TV Series with folder-based navigation.
- **Episode Playback**: View episodes in a refined grid layout and play them.
- **Movie Details**: See detailed information including poster, backdrop, rating, release date, and overview.
- **Smooth D-pad Navigation**: Optimized Leanback framework implementation with perfect focus handling.
- **TMDB Integration**: Real-time movie and series data from The Movie Database API.
- **Local Library**: Browse and play content from your local server.

### Technical Improvements

#### 1. **Enhanced Navigation**
- Preserved excellent Leanback D-pad navigation.
- Smooth focus transitions with scale animations.
- **Navbar Integration**: Seamless navigation between Home, Movies, Series, and Search.
- **Custom Focus Handling**: Specialized logic for navigating between fragments and the navbar.

#### 2. **Better Architecture**
- Parallel API loading for faster performance.
- Proper coroutine usage with Dispatchers.IO.
- Comprehensive error handling and logging.
- **Skeleton Loading**: Custom skeleton screens for smoother loading experiences.

#### 3. **Improved UI/UX**
- **Modern Card Design**:
    - Edge-to-edge images.
    - Subtle rounded corners (5dp).
    - Refined typography and spacing.
- **Series Section**:
    - Folder-based structure for series and seasons.
    - 3-row skeleton loading state.
    - Optimized grid layout for episodes (4 cards per row).
- Gradient overlays for better text readability.
- Smooth animations on focus changes.

#### 4. **Error Handling**
- Network timeout configuration (30s).
- Retry on connection failure.
- Graceful error messages via Toast.
- Comprehensive logging for debugging.

#### 5. **Performance**
- Parallel loading of all categories.
- Proper image caching with Glide.
- Efficient memory management.

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Android Leanback (for TV)
- **Architecture**: Repository pattern with coroutines
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Glide
- **Async**: Kotlin Coroutines
- **API**: TMDB (The Movie Database) & Local Server

## Key Components

### MainActivity
- BrowseSupportFragment for main browsing interface.
- Hosts the main navigation bar.
- Manages fragment transactions for Home, Movies, Series, and Search.

### SeriesActivity & SeriesDetailsFragment
- Dedicated activity for TV Series.
- **SeriesDetailsFragment**: Handles the display of episodes in a grid.
- Custom `EpisodePresenter` for rendering episode cards.
- Implements `handleUpKey` for seamless D-pad navigation back to the navbar.

### Presenters
- **MovieCardPresenter**: Custom presenter for movie cards with focus animations.
- **EpisodePresenter**: Specialized presenter for episode items with edge-to-edge design.
- **SkeletonPresenter**: Handles loading states with pulsing animations.

### Repositories
- **MovieRepository**: Fetches data from TMDB.
- **LibraryRepository**: Manages local library content, including folder navigation for series.

## D-pad Navigation Features

The app maintains excellent D-pad navigation:
- **Smooth scrolling** through rows and grids.
- **Focus persistence** when navigating between sections.
- **Navbar Navigation**: Easy access to main sections via the top navigation bar.
- **Back button support** throughout the app.

## Build & Run

1. Open project in Android Studio.
2. Sync Gradle dependencies.
3. Run on Android TV emulator or device.
4. Navigate using D-pad or arrow keys.

## Requirements

- Android SDK 21+
- Target SDK 34
- Android TV device or emulator
- Internet connection for TMDB API
- Local server running (for Library content)

## API Key

The app uses TMDB API. Current key is included for testing purposes.
For production, replace with your own key in `MovieRepository.kt`.

## Future Enhancements

- Watchlist/favorites
- Video trailers
- Cast information
- Similar movies recommendations
- Multiple language support

