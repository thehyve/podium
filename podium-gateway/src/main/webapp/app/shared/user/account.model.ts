export class Account {
    constructor(
        public activated: boolean,
        public authorities: string[],
        public email: string,
        public telephone: string,
        public institute: string,
        public department: string,
        public jobTitle: string,
        public specialism: string,
        public firstName: string,
        public langKey: string,
        public lastName: string,
        public login: string,
        public imageUrl: string
    ) { }
}
