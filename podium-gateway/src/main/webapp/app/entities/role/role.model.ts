import { User } from '../../shared';
import { Organisation } from '../organisation';

export class Role {
    constructor(
        public id?: number,
        /**
         * UUID of the organisation (only for organisation roles).
         */
        public organisation?: string,
        /**
         * Authority token, e.g., ROLE_ORGANISATION_ADMIN.
         */
        public authority?: string,
        /**
         * UUIDs of users for this role.
         */
        public users?: string[]
    ) { }
}
