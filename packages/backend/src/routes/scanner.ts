import type { FastifyInstance } from 'fastify';
import { verifyAuth, verifyAdmin } from '../hooks/auth.js';
import { scanLibrary, getScanStatus } from '../services/scanner.js';
import { fetchAllMetadata } from '../services/igdb.js';

export async function scannerRoutes(fastify: FastifyInstance) {
  fastify.addHook('onRequest', verifyAuth);
  fastify.addHook('onRequest', verifyAdmin);

  // Start a scan (optionally fetch metadata after)
  fastify.post('/run', async (_request, reply) => {
    const status = getScanStatus();
    if (status.state === 'running') {
      return reply.status(409).send({ error: 'SCAN_RUNNING', message: 'A scan is already in progress' });
    }

    // Run scan, then auto-fetch metadata for new games
    scanLibrary(fastify)
      .then(async () => {
        fastify.log.info('Scan complete, fetching IGDB metadata...');
        const result = await fetchAllMetadata(fastify);
        fastify.log.info(`IGDB metadata: fetched ${result.fetched}/${result.total} games`);
      })
      .catch((err) => {
        fastify.log.error(err, 'Scan/metadata failed');
      });

    return { message: 'Scan started' };
  });

  // Get scan status
  fastify.get('/status', async () => {
    return getScanStatus();
  });

  // Batch fetch IGDB metadata for all unmatched games
  fastify.post('/fetch-metadata', async () => {
    fetchAllMetadata(fastify).catch((err) => {
      fastify.log.error(err, 'Batch metadata fetch failed');
    });
    return { message: 'Metadata fetch started' };
  });
}
