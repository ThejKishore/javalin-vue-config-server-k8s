/**
 * API Client for communicating with the backend ConfigServer API
 */
class ApiClient {
    static BASE_URL = '/api/config';

    /**
     * Get properties for a specific application and domain
     */
    static async getProperties(domain, application) {
        const response = await fetch(
            `${this.BASE_URL}/properties/${domain}/${application}`
        );

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to fetch properties');
        }

        return response.json();
    }

    /**
     * Get configuration sync info
     */
    static async getConfigSync(domain, application) {
        const response = await fetch(
            `${this.BASE_URL}/sync/${domain}/${application}`
        );

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to fetch sync info');
        }

        return response.json();
    }

    /**
     * Get configuration as YAML
     */
    static async getConfigYaml(domain, application) {
        const response = await fetch(
            `${this.BASE_URL}/yml/${domain}/${application}`
        );

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to fetch YAML config');
        }

        return response.text();
    }

    /**
     * Update properties with optimistic locking
     */
    static async updateProperties(domain, application, properties, expectedVersion, expectedUpdatedTm) {
        const payload = {
            properties,
            expectedVersionNumber: expectedVersion,
            expectedUpdatedTm,
            updatedBy: 'admin' // In a real app, this would come from user context
        };

        const response = await fetch(
            `${this.BASE_URL}/properties/${domain}/${application}`,
            {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            }
        );

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to update properties');
        }

        return response.json();
    }

    /**
     * Add a new property
     */
    static async addProperty(domain, application, key, value) {
        const payload = {
            propertyKey: key,
            propertyValue: value,
            createdBy: 'admin' // In a real app, this would come from user context
        };

        const response = await fetch(
            `${this.BASE_URL}/properties/${domain}/${application}`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            }
        );

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to add property');
        }

        return response.json();
    }

    /**
     * Get audit history
     */
    static async getAuditHistory(domain, application, limit = 100) {
        const response = await fetch(
            `${this.BASE_URL}/audit/${domain}/${application}?limit=${limit}`
        );

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to fetch audit history');
        }

        return response.json();
    }

    /**
     * Get metadata
     */
    static async getMetadata() {
        const response = await fetch(`${this.BASE_URL}/meta`);

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to fetch metadata');
        }

        return response.json();
    }

    /**
     * Delete a property by key
     */
    static async deleteProperty(domain, application, propertyKey) {
        const response = await fetch(
            `${this.BASE_URL}/properties/${domain}/${application}/${propertyKey}`,
            {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        );

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to delete property');
        }

        return response.json();
    }

    /**
     * Onboard a new service with configuration file
     */
    static async onboardService(domain, application, file) {
        const formData = new FormData();
        formData.append('domain', domain);
        formData.append('application', application);
        formData.append('file', file);

        const response = await fetch(
            `${this.BASE_URL}/onboard`,
            {
                method: 'POST',
                body: formData
            }
        );

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to onboard service');
        }

        return response.json();
    }

    /**
     * Search domains by name
     */
    static async searchDomains(query) {
        const response = await fetch(
            `${this.BASE_URL}/search/domains?q=${encodeURIComponent(query)}`
        );

        if (!response.ok) {
            // If search endpoint doesn't exist, return empty array
            return [];
        }

        const data = await response.json();
        return data.domains || [];
    }

    /**
     * Search applications by domain and name
     */
    static async searchApplications(domain, query) {
        const response = await fetch(
            `${this.BASE_URL}/search/applications/${domain}?q=${encodeURIComponent(query)}`
        );

        if (!response.ok) {
            // If search endpoint doesn't exist, return empty array
            return [];
        }

        const data = await response.json();
        return data.applications || [];
    }
}

export default ApiClient;

