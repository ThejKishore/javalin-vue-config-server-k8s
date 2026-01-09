import Sidebar from './Sidebar.js';
import ConfigManagement from './ConfigManagement.js';
import AuditHistory from './AuditHistory.js';

export default {
    template: `
        <div class="flex h-screen bg-gray-50">
            <!-- Sidebar -->
            <Sidebar @navigate="currentView = $event" />
            
            <!-- Main Content -->
            <div class="flex-1 flex flex-col">
                <!-- Header -->
                <header class="bg-white shadow">
                    <div class="px-8 py-4">
                        <h1 class="text-2xl font-bold text-gray-900">Platform Management</h1>
                    </div>
                </header>
                
                <!-- Content -->
                <main class="flex-1 overflow-auto p-8">
                    <ConfigManagement v-if="currentView === 'config'" />
                    <AuditHistory v-if="currentView === 'audit'" />
                </main>
            </div>
        </div>
    `,
    components: {
        Sidebar,
        ConfigManagement,
        AuditHistory
    },
    data() {
        return {
            currentView: 'config'
        };
    }
};

