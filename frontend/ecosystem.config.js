// aws ec2 pm2 설정

module.exports = {
    apps: [
        {
            name: 'frontend',
            script: 'npm',
            args: 'start',
            cwd: '/home/ubuntu/NBE6-8-2-Team06/frontend',
            env: {
                NODE_ENV: 'production',
                PORT: 3000
            },
            instances: 1,
            autorestart: true,
            watch: false,
            max_memory_restart: '1G'
        }
    ]
};