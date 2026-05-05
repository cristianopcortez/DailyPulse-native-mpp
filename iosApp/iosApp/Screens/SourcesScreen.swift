//
//  SourcesScreen.swift
//  iosApp
//
//  Created by Petros Efthymiou on 01/12/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

extension SourcesScreen {

    @MainActor
    class SourcesViewModelWrapper: ObservableObject {

        private var stateCollector: CloseableFlowCollector?

        init() {
            let vm = SourcesInjector().sourcesViewModel
            self.viewModel = vm
            self.sourcesState = vm.sourcesState.value as! SourcesState
        }

        let viewModel: SourcesViewModel

        @Published var sourcesState: SourcesState

        func startObserving() {
            stateCollector?.cancel()
            stateCollector = viewModel.collectSourcesStateForSwift { [weak self] state in
                guard let self else { return }
                Task { @MainActor in
                    self.sourcesState = state
                }
            }
        }

        func stopObserving() {
            stateCollector?.cancel()
            stateCollector = nil
        }
    }
}

struct SourcesScreen: View {
    @Environment(\.dismiss)
    private var dismiss

    @ObservedObject private(set) var viewModel: SourcesScreen.SourcesViewModelWrapper

    var body: some View {
        NavigationStack {
            VStack {

                if let error = viewModel.sourcesState.error {
                    ErrorMessage(message: error)
                }

                if viewModel.sourcesState.loading {
                    Loader()
                }

                if !viewModel.sourcesState.sources.isEmpty {
                    ScrollView {
                        LazyVStack(spacing: 10) {
                            ForEach(viewModel.sourcesState.sources, id: \.self) { source in
                                SourceItemView(name: source.name, desc: source.desc, origin: source.origin)
                            }
                        }
                    }
                }
            }
            .onAppear {
                self.viewModel.startObserving()
            }
            .onDisappear {
                self.viewModel.stopObserving()
                self.viewModel.viewModel.clear()
            }
            .navigationTitle("Sources")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        dismiss()
                    } label: {
                        Text("Done")
                            .bold()
                    }
                }
            }
        }
    }
}

struct SourceItemView: View {
    let name: String
    let desc: String
    let origin: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(name)
                .font(.title)
                .fontWeight(.bold)
            Text(desc)
            Text(origin).frame(maxWidth: .infinity, alignment: .trailing).foregroundStyle(.gray)
        }
        .padding(16)
    }
}
