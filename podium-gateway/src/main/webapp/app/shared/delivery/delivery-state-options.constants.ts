/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

// Every DeliveryState has multiple options available for each RequestType
export const DeliveryStateOptions: any = {
    'primary': {
        'Preparation': {
            'Data': {
                'label': 'Release data',
                'action': 'release'
            },
            'Material': {
                'label': 'Release material',
                'action': 'release'
            },
            'Images': {
                'label': 'Release images',
                'action': 'release'
            }
        },
        'Released': {
            'Data': {
                'label': 'Mark data received',
                'action': 'received'
            },
            'Material': {
                'label': 'Mark material received',
                'action': 'received'
            },
            'Images': {
                'label': 'Mark images received',
                'action': 'received'
            }
        }
    },
    'secondary': {
        'Preparation': {
            'Data': [
                {
                    'label': 'Cancel data delivery',
                    'action': 'cancel'
                }
            ],
            'Material': [
                {
                    'label': 'Cancel material delivery',
                    'action': 'cancel'
                }
            ],
            'Images': [
                {
                    'label': 'Cancel images delivery',
                    'action': 'cancel'
                }
            ]
        },
        'Released': {
            'Data': [
                {
                    'label': 'Cancel data delivery',
                    'action': 'cancel'
                }
            ],
            'Material': [
                {
                    'label': 'Cancel material delivery',
                    'action': 'cancel'
                }
            ],
            'Images': [
                {
                    'label': 'Cancel images delivery',
                    'action': 'cancel'
                }
            ]
        }
    },
    'icons': {
        'type': {
            'Data': 'dns',
            'Material': 'blur_on',
            'Images': 'image'
        },
        'status': {
            'Preparation': 'local_grocery_store',
            'Released': 'local_shipping',
            'Received': 'done',
            'Closed': 'cancel'
        }
    }

};
