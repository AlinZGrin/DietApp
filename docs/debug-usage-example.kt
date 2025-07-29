// Example: How to use DatabaseDebugger in your ChartsViewModel

// 1. Add to your ChartsViewModel constructor:
@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val foodRepository: FoodRepository,
    private val weightRepository: WeightRepository,
    private val databaseDebugger: DatabaseDebugger // Add this
) : ViewModel() {

    // 2. Add a debug function you can call:
    fun debugDatabase() {
        viewModelScope.launch {
            authState.collect { state ->
                if (state is AuthState.Authenticated) {
                    databaseDebugger.printDatabaseSummary(state.userId)
                    databaseDebugger.printTodaysFoodLogs(state.userId)
                }
            }
        }
    }

    // 3. Add a function to search foods in local database:
    fun debugSearchFoods(query: String) {
        viewModelScope.launch {
            databaseDebugger.searchFoodInDatabase(query)
        }
    }

    // 4. Add function to print all foods:
    fun debugPrintAllFoods() {
        viewModelScope.launch {
            databaseDebugger.printAllFoods()
        }
    }
}

// Usage in your UI (for testing):
// Call these functions from your Compose screens or add debug buttons

// In ChartsScreen.kt, add temporary debug buttons:
Button(onClick = { viewModel.debugDatabase() }) {
    Text("Debug Database")
}

Button(onClick = { viewModel.debugSearchFoods("apple") }) {
    Text("Search Apple")
}

Button(onClick = { viewModel.debugPrintAllFoods() }) {
    Text("Print All Foods")
}

// Check Android Studio Logcat for output
