import { User } from '../../shared';
import { Organisation } from '../organisation';
export class Role {
    constructor(
        public id?: number,
        public users?: User,
        public organisation?: Organisation,
    ) { }
}
