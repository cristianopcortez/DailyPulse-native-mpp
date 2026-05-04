extension ArticlesScreen {
    
    @MainActor
    class ArticlesViewModelWrapper: ObservableObject {
        
        let articlesViewModel: ArticlesViewModel
        
        init() {
            let vm = ArticlesInjector().articlesViewModel
            self.articlesViewModel = vm
            // Fix: Explicitly cast Any? to ArticlesState
            self.articlesState = vm.articlesState.value as! ArticlesState
        }
        
        @Published var articlesState: ArticlesState
        
        func startObserving() {
            Task {
                // Fix: Access the sequence through the common interop bridge
                // If you are not using SKIE, you might need to cast the flow 
                // or use a helper method from your shared module.
                for await articlesS in articlesViewModel.articlesState {
                    if let state = articlesS as? ArticlesState {
                        self.articlesState = state
                    }
                }
            }
        }
    }
}