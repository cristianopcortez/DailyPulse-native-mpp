//
//  NativeRootView.swift
//  iosApp
//
//  Root view for the **native** UI flavor.
//
//  Wires up SwiftUI navigation between the SwiftUI screens defined under
//  iosApp/Screens. Only the business layer (ViewModels, use cases, repos…)
//  is consumed from the shared KMP framework.
//

import SwiftUI
import shared

struct NativeRootView: View {
    @State private var showSources = false
    @State private var showAbout = false

    var body: some View {
        NavigationStack {
            ArticlesScreen(viewModel: .init())
                .toolbar {
                    ToolbarItemGroup(placement: .primaryAction) {
                        Button {
                            showSources = true
                        } label: {
                            Image(systemName: "list.bullet")
                        }
                        Button {
                            showAbout = true
                        } label: {
                            Image(systemName: "info.circle")
                        }
                    }
                }
                .sheet(isPresented: $showSources) {
                    SourcesScreen(viewModel: .init())
                }
                .sheet(isPresented: $showAbout) {
                    AboutScreen()
                }
        }
    }
}
