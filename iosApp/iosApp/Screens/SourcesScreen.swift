extension SourcesScreen {
    
    @MainActor
    class SourcesViewModelWrapper: ObservableObject {
        
        init() {
            let vm = SourcesInjector().sourcesViewModel
            self.viewModel = vm
            // Fix: Explicit cast from Any? to SourcesState for Xcode 26.4
            self.sourcesState = vm.sourcesState.value as! SourcesState
        }
        
        let viewModel: SourcesViewModel
        
        @Published var sourcesState: SourcesState
        
        func startObserving() {
            Task {
                // Fix: Handle type-safe flow observation
                for await sourcesS in viewModel.sourcesState {
                    if let state = sourcesS as? SourcesState {
                        self.sourcesState = state
                    }
                }
            }
        }
    }
}